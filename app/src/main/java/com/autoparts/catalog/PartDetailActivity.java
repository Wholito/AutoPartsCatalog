package com.autoparts.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PartDetailActivity extends BaseActivity {

    public static final String EXTRA_PART_ID = "part_id";

    private EditText editTitle, editDescription, editDate;
    private MaterialButton btnSave, btnDelete;
    private DatabaseHelper db;
    private long partId = -1;
    private boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        partId = getIntent().getLongExtra(EXTRA_PART_ID, -1);
        isNew = partId < 0;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isNew ? R.string.new_part : R.string.edit_part);
        }

        db = new DatabaseHelper(this);
        editTitle = findViewById(R.id.edit_title);
        editDescription = findViewById(R.id.edit_description);
        editDate = findViewById(R.id.edit_date);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);

        if (isNew) {
            btnDelete.setVisibility(android.view.View.GONE);
            editDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        } else {
            loadPart();
        }

        btnSave.setOnClickListener(v -> savePart());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadPart() {
        Part p = db.getPart(partId);
        if (p != null) {
            editTitle.setText(p.getTitle());
            editDescription.setText(p.getDescription());
            editDate.setText(p.getDate());
        }
    }

    private void savePart() {
        String title = editTitle.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();
        String date = editDate.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.part_title, Toast.LENGTH_SHORT).show();
            return;
        }

        Part part = new Part(title, desc, date);
        if (isNew) {
            db.insertPart(part);
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
        } else {
            part.setId(partId);
            db.updatePart(part);
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    db.deletePart(partId);
                    Toast.makeText(this, R.string.delete, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
