package com.example.haeyagym;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class TimerActivity extends AppCompatActivity {

    public static final int RESULT_ADD = 2;
    public static final int RESULT_START = 3;

    private int setCount = 5;
    private int exerciseMin = 3, exerciseSec = 0;
    private int breakMin = 0, breakSec = 30;

    private int maxRoutineListCount;

    private DBHelper dbHelper;
    SQLiteDatabase db;

    private TextView textCount;
    private TextView textExer;
    private TextView textBreak;
    private ListView routineList;
    private ArrayList<HashMap<String, String>> routineListData;

    private SimpleAdapter simpleAdapter;

    private ActivityResultContract<Intent, ActivityResult> resultContract;
    private ActivityResultCallback<ActivityResult> resultCallback;
    private ActivityResultLauncher<Intent> routineLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        textCount = findViewById(R.id.textSetCount);
        textExer = findViewById(R.id.textTimerCount);
        textBreak = findViewById(R.id.textBreakCount);
        routineList = findViewById(R.id.routineList);

        dbHelper = new DBHelper(this);
        routineListData = new ArrayList<>();

        simpleAdapter = new SimpleAdapter(this, routineListData, R.layout.exercise_item, new String[] {"exerName", "setCount", "exerTime", "breakTime"},
                new int[] {R.id.textItemExerciseName, R.id.textItemSetCount, R.id.textItemExerciseTime, R.id.textItemBreakTime});

        routineListData.clear();
        routineList.setAdapter(simpleAdapter);

        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(DBContract.SQL_LOAD, null);
        int i = 0;
        while(cursor.moveToNext()) {
            HashMap<String, String> temp = new HashMap<>();

            temp.put("id", cursor.getString(0));
            temp.put("exerName", cursor.getString(1));
            temp.put("setCount", cursor.getString(2));
            temp.put("exerTime", cursor.getString(3));
            temp.put("breakTime", cursor.getString(4));
            routineListData.add(temp);
            maxRoutineListCount = Math.max(maxRoutineListCount, Integer.parseInt(routineListData.get(i).get("id")));
            i++;
        }
        cursor.close();

        simpleAdapter.notifyDataSetChanged();

        //============================================================================== ????????? ??????



        resultContract = new ActivityResultContracts.StartActivityForResult();
        resultCallback = new ActivityResultCallback<ActivityResult>() {             //???????????? ?????????, ????????? ????????? ?????? ??? ?????? ????????????
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_ADD){
                    Intent intent = result.getData();
                    HashMap<String, String> temp = new HashMap<>();
                    maxRoutineListCount++;

                    temp.put("id", String.valueOf(maxRoutineListCount));
                    temp.put("exerName", intent.getStringExtra("exerName"));
                    temp.put("setCount", intent.getStringExtra("setCount"));
                    temp.put("exerTime", intent.getStringExtra("exerTime"));
                    temp.put("breakTime", intent.getStringExtra("breakTime"));

                    ContentValues values = new ContentValues();
                    values.put("ID", String.valueOf(maxRoutineListCount));
                    values.put("NAME", intent.getStringExtra("exerName"));
                    values.put("COUNT", intent.getStringExtra("setCount"));
                    values.put("EXER", intent.getStringExtra("exerTime"));
                    values.put("BREAK", intent.getStringExtra("breakTime"));
                    routineListData.add(temp);

                    db = dbHelper.getWritableDatabase();
                    db.insert(DBContract.TABLE_NAME, null, values);
                    simpleAdapter.notifyDataSetChanged();
                }
                else if(result.getResultCode() == RESULT_START){
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
            }
        };
        routineLauncher = registerForActivityResult(resultContract, resultCallback);
        //==============================================================================

        routineList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Toast.makeText(getApplicationContext(), routineListData.get(i).get("id"), Toast.LENGTH_LONG).show();
                db.delete(DBContract.TABLE_NAME, "ID=?", new String[] {routineListData.get(i).get("id")});
                routineListData.remove(i);
                simpleAdapter.notifyDataSetChanged();

            }
        });

    }

    public void SetCountPlus(View view) {               //?????? ???????????? ???????????? ?????????, 10?????? ??????
        if(setCount < 10){
            setCount++;
        }
        textCount.setText(String.valueOf(setCount));
    }

    public void SetCountMinus(View view) {              //?????? ???????????? ???????????? ?????????, 1?????? ??????
        if(setCount > 1)
            setCount--;
        textCount.setText(String.valueOf(setCount));
    }

    public void TimerPlus(View view) {                  //?????? ????????? ???????????? ?????????, 30?????? ???????????? ?????? ????????? X
        exerciseSec += 30;

        if(exerciseSec == 60){
            exerciseMin++;
            exerciseSec = 0;
        }
        textExer.setText(String.valueOf(exerciseMin) + ':' + (exerciseSec == 0 ? "00" : "30"));

    }

    public void TimerMinus(View view) {                 //?????? ????????? ???????????? ?????????, ?????? ?????? 30???
        if(exerciseMin <= 0 && exerciseSec == 30)
            return;


        exerciseSec -= 30;
        if(exerciseSec < 0){
            exerciseMin -= 1;
            exerciseSec = 30;
        }
        textExer.setText(String.valueOf(exerciseMin) + ':' + (exerciseSec == 0 ? "00" : "30"));
    }

    public void BreakPlus(View view) {                  //?????? ????????? ???????????? ?????????, ????????? X
        breakSec += 30;

        if(breakSec == 60){
            breakMin++;
            breakSec = 0;
        }

        textBreak.setText(String.valueOf(breakMin) + ':' + (breakSec == 0 ? "00" : "30"));
    }

    public void BreakMinus(View view) {                 //?????? ????????? ???????????? ?????????, ?????? ?????? 30???
        if(breakMin <= 0 && breakSec == 30)
            return;

        breakSec -= 30;
        if(breakSec < 0){
            breakMin -= 1;
            breakSec = 30;
        }
        textBreak.setText(String.valueOf(breakMin) + ':' + (breakSec == 0 ? "00" : "30"));
    }

    public void TimerStart(View view){
        Intent intent = new Intent(getApplicationContext(), StartRoutineActivity.class);
        intent.putExtra("setCount", setCount);
        intent.putExtra("exerMin", exerciseMin);
        intent.putExtra("exerSec", exerciseSec);
        intent.putExtra("breakMin", breakMin);
        intent.putExtra("breakSec", breakSec);
        routineLauncher.launch(intent);

    }

    public void AddRoutine(View view){
        Intent intent = new Intent(getApplicationContext(), AddRoutineActivity.class);
        routineLauncher.launch(intent);
    }
}