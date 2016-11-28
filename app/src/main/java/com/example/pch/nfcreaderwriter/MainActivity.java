package com.example.pch.nfcreaderwriter;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    Switch my_switch;
    TextView txt_mode,txt_read;
    EditText edit_write;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        my_switch=(Switch)findViewById(R.id.my_switch);
        txt_mode=(TextView)findViewById(R.id.txt_mode);
        edit_write=(EditText)findViewById(R.id.edit_write);
        txt_read=(TextView)findViewById(R.id.txt_read);

        my_switch.setOnCheckedChangeListener(this);

        //Ndef형식의 태그가 발견되면 시스템이 필터에 의해 우리에게 그 정보를 전달해준다. 그 정보를 추출해 보자!!
        Intent intent=getIntent();

        getInfo(intent);
    }
    public void getInfo(Intent intent){
        String action=intent.getAction();

        //지금 전달된 인텐트가 표준 nfc인 경우
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){

            Parcelable[] parcelables=intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            //인텐트에 넣기 위해 Parcelable 화된 NdefMessage를 다시 복원하자
            NdefMessage[] messages=new NdefMessage[parcelables.length];

            for(int i=0;i<messages.length;i++){
                messages[i]=(NdefMessage) parcelables[i];

                //하나의 메세지는 복수개의 레코드를 보유할 수 있다.
                NdefRecord[] records=messages[i].getRecords();
                for(int a=0;a<records.length;a++){
                    String data=new String(records[a].getPayload());
                    txt_read.append(data+"\n");
                }
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            txt_mode.setText("쓰기모드");
            /*Toast.makeText(this, "스위치 상태는" + b, Toast.LENGTH_SHORT).show();*/
        }else{
            txt_mode.setText("읽기모드");
        }
    }
    /*-----------------------------------------
        NFC태그에 데이터 쓰기!
    -----------------------------------------*/
    public NdefRecord createTextRecord(String payload, boolean encodeInUtf8) {
        byte[] langBytes = Locale.getDefault().getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }
    public void writeTag(){
        //작성한 내용이 있을경우 태그를 대기하도록 한다.

        //레코드 생성!
        String msg=edit_write.getText().toString();
        NdefRecord record=createTextRecord(msg,true);

        //메시지 생성!
        NdefMessage message=new NdefMessage(new NdefRecord[]{record});

    }

    public void btnClick(View view){
        if(txt_mode.getText().toString().equals("쓰기모드")){
            writeTag();
        }
    }
}
