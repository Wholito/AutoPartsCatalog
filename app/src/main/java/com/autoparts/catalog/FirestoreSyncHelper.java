package com.autoparts.catalog;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class FirestoreSyncHelper {

    public interface Callback {
        void onDone(String message);

        void onError(String message);
    }

    private static final String COLLECTION = "parts";
    private static final Executor IO = Executors.newSingleThreadExecutor();

    private FirestoreSyncHelper() {
    }

    public static void syncUp(Context context, DatabaseHelper db, Callback callback) {
        IO.execute(() -> {
            try {
                ensureFirebaseConfigured(context);
                FirebaseFirestore fs = FirebaseFirestore.getInstance();
                List<Part> parts = db.getAllParts();
                if (parts.isEmpty()) {
                    post(context, () -> callback.onDone(context.getString(R.string.firestore_sync_no_local_data)));
                    return;
                }
                List<Task<?>> tasks = new ArrayList<>();
                for (Part p : parts) {
                    tasks.add(uploadOne(fs, db, p));
                }
                Tasks.await(Tasks.whenAll(tasks));
                post(context, () -> callback.onDone(context.getString(R.string.firestore_sync_up_ok)));
            } catch (Exception e) {
                String msg = explainSyncError(context, e);
                post(context, () -> callback.onError(
                        context.getString(R.string.firestore_sync_error, msg)));
            }
        });
    }

    public static void syncDown(Context context, DatabaseHelper db, Callback callback) {
        IO.execute(() -> {
            try {
                ensureFirebaseConfigured(context);
                FirebaseFirestore fs = FirebaseFirestore.getInstance();
                List<DocumentSnapshot> docs = Tasks.await(fs.collection(COLLECTION).get()).getDocuments();
                for (DocumentSnapshot doc : docs) {
                    applyRemoteDocument(db, doc);
                }
                post(context, () -> callback.onDone(context.getString(R.string.firestore_sync_down_ok)));
            } catch (Exception e) {
                String msg = explainSyncError(context, e);
                post(context, () -> callback.onError(
                        context.getString(R.string.firestore_sync_error, msg)));
            }
        });
    }

    private static void post(Context ctx, Runnable r) {
        android.os.Handler main = new android.os.Handler(ctx.getMainLooper());
        main.post(r);
    }

    private static void ensureFirebaseConfigured(Context context) {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            FirebaseOptions options = app.getOptions();
            String projectId = options != null ? options.getProjectId() : null;
            if (projectId == null || projectId.trim().isEmpty() || projectId.contains("placeholder")) {
                throw new IllegalStateException(context.getString(R.string.firestore_not_configured));
            }
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                throw e;
            }
            throw new IllegalStateException(context.getString(R.string.firestore_not_configured));
        }
    }

    private static String explainSyncError(Context context, Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException fe = (FirebaseFirestoreException) root;
            if (fe.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return context.getString(R.string.firestore_permission_denied);
            }
            String m = fe.getMessage();
            if (m != null && !m.trim().isEmpty()) {
                return fe.getCode().name() + ": " + m;
            }
            return fe.getCode().name();
        }
        String msg = root.getMessage();
        if (msg != null && !msg.trim().isEmpty()) {
            return msg;
        }
        return root.toString();
    }

    private static Task<Void> uploadOne(FirebaseFirestore fs, DatabaseHelper db, Part p) {
        Map<String, Object> map = toMap(p);
        String rid = p.getRemoteId();
        if (rid != null && !rid.isEmpty()) {
            return fs.collection(COLLECTION).document(rid).set(map, SetOptions.merge())
                    .continueWith(t -> {
                        if (!t.isSuccessful()) {
                            Exception e = t.getException();
                            throw new RuntimeException(e != null ? e : new IllegalStateException());
                        }
                        return null;
                    });
        }
        return fs.collection(COLLECTION).add(map).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                throw new RuntimeException(e != null ? e : new IllegalStateException());
            }
            DocumentReference ref = task.getResult();
            if (ref != null) {
                db.updatePartRemoteId(p.getId(), ref.getId());
            }
            return Tasks.forResult(null);
        });
    }

    private static void applyRemoteDocument(DatabaseHelper db, DocumentSnapshot doc) {
        String rid = doc.getId();
        String title = str(doc.get("title"));
        String description = str(doc.get("description"));
        String date = str(doc.get("date"));
        String category = str(doc.get("category"));
        String imageUrl = str(doc.get("imageUrl"));
        long localId = longVal(doc.get("localId"));

        Part byRemote = db.getPartByRemoteId(rid);
        if (byRemote != null) {
            Part u = new Part(byRemote.getId(), title, description, date, category, imageUrl, rid);
            db.updatePart(u);
            return;
        }
        if (localId > 0) {
            Part local = db.getPart(localId);
            if (local != null) {
                Part u = new Part(localId, title, description, date, category, imageUrl, rid);
                db.updatePart(u);
                return;
            }
        }
        Part insert = new Part(title, description, date);
        insert.setCategory(category);
        insert.setImageUrl(imageUrl);
        long newId = db.insertPart(insert);
        db.updatePartRemoteId(newId, rid);
    }

    private static Map<String, Object> toMap(Part p) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", p.getTitle());
        m.put("description", p.getDescription());
        m.put("date", p.getDate());
        m.put("category", p.getCategory());
        m.put("imageUrl", p.getImageUrl());
        m.put("localId", p.getId());
        return m;
    }

    private static String str(Object o) {
        return o != null ? String.valueOf(o) : "";
    }

    private static long longVal(Object o) {
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        }
        if (o instanceof Double) {
            return ((Double) o).longValue();
        }
        return -1L;
    }
}
