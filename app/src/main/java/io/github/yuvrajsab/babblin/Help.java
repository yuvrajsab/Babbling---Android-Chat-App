package io.github.yuvrajsab.babblin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Help extends AppCompatActivity {

    String[] item = {"FAQs", "Feedback", "Report Problem"};
    ListView helpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        helpList = findViewById(R.id.helpList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, item);
        helpList.setAdapter(arrayAdapter);

        helpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent faqIntent = new Intent(Help.this, FAQ.class);
                        startActivity(faqIntent);
                        break;
                    case 1:
                        Intent feedbackIntent = new Intent(Help.this, Feedback.class);
                        startActivity(feedbackIntent);
                        break;
                    case 2:
                        Intent reportIntent = new Intent(Help.this, ReportProblem.class);
                        startActivity(reportIntent);
                        break;
                }
            }
        });
    }
}
