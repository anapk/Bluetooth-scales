package com.test.BTClient;


//import BTClient;

//import java.text.SimpleDateFormat;
//import java.util.Date;
//import org.achartengine.chartdemo.*;
//import org.achartengine.chartdemo.demo.*;
//import org.achartengine.chartdemo.demo.chart.AverageTemperatureChart;
//import org.achartengine.chartdemo.demo.chart.IDemoChart;


import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;
import java.util.UUID;


import com.test.BTClient.DeviceListActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;            //��ʹ�ò˵����������
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BTClient extends Activity {

	private final static int REQUEST_CONNECT_DEVICE = 1;    //�궨���ѯ�豸���

	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP����UUID��

	private InputStream is;    //������������������������
	//private TextView text0;    //��ʾ������
	//private EditText edit0;    //��������������
	private TextView dis;       //����������ʾ���
	//	private ScrollView sv;      //��ҳ���
	private String w_value = "";    //��ʾ����
	//	private String fmsg = "";    //���������ݻ���
	private String h_value = ""; // ��ʾ���
	private String BMI_value = "";
	private String w_save = "";
	private String h_save = "";

	int[] weight = new int[100];// ��ȡ�ļ��������������
	int[] height = new int[100];// ��ȡ�ļ�������������
	float[] BMI = new float[100];// BMIָ�� = kg/(m^2)

	public String filename=""; //��������洢���ļ���
	BluetoothDevice _device = null;     //�����豸
	BluetoothSocket _socket = null;      //����ͨ��socket
	boolean _discoveryFinished = false;    
	boolean bRun = true;
	boolean bThread = false;

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //��ȡ�����������������������豸

	//private TextView dis2;
	//private TextView dis3;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);   //���û���Ϊ������ main.xml

		//text0 = (TextView)findViewById(R.id.Text0);  //�õ���ʾ�����
		//	edit0 = (EditText)findViewById(R.id.Edit0);   //�õ��������
		//  sv = (ScrollView)findViewById(R.id.ScrollView01);  //�õ���ҳ���
		dis = (TextView) findViewById(R.id.weight);      //�õ�������ʾ���
		//		dis2 = (TextView) findViewById(R.id.height); 
		//����򿪱��������豸���ɹ�����ʾ��Ϣ����������
		if (_bluetooth == null){
			Toast.makeText(this, "�޷����ֻ���������ȷ���ֻ��Ƿ����������ܣ�", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// �����豸���Ա�����  
		new Thread(){
			public void run(){
				if(_bluetooth.isEnabled()==false){
					_bluetooth.enable();
				}
			}   	   
		}.start();      
	}

	//���Ͱ�����Ӧ
	/*
	public void onSendButtonClicked(View v){
		int i=0;
		int n=0;
		try{
			OutputStream os = _socket.getOutputStream();   //�������������
			byte[] bos = edit0.getText().toString().getBytes();
			for(i=0;i<bos.length;i++){
				if(bos[i]==0x0a)n++;
			}
			byte[] bos_new = new byte[bos.length+n];
			n=0;
			for(i=0;i<bos.length;i++){ //�ֻ��л���Ϊ0a,�����Ϊ0d 0a���ٷ���
				if(bos[i]==0x0a){
					bos_new[n]=0x0d;
					n++;
					bos_new[n]=0x0a;
				}else{
					bos_new[n]=bos[i];
				}
				n++;
			}

			os.write(bos_new);	
		}catch(IOException e){  		
		}  	
	}*/

	//���ջ�������ӦstartActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_CONNECT_DEVICE:     //���ӽ������DeviceListActivity���÷���
			// ��Ӧ���ؽ��
			if (resultCode == Activity.RESULT_OK) {   //���ӳɹ�����DeviceListActivity���÷���
				// MAC��ַ����DeviceListActivity���÷���
				String address = data.getExtras()
						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ������豸���      
				_device = _bluetooth.getRemoteDevice(address);

				// �÷���ŵõ�socket
				try{
					_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
				}catch(IOException e){
					Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
				}
				//����socket
				Button btn = (Button) findViewById(R.id.Button03);
				try{
					_socket.connect();
					Toast.makeText(this, "����"+_device.getName()+"�ɹ���", Toast.LENGTH_SHORT).show();
					btn.setText("�Ͽ�");
				}catch(IOException e){
					try{
						Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
						_socket.close();
						_socket = null;
					}catch(IOException ee){
						Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
					}

					return;
				}

				//�򿪽����߳�
				try{
					is = _socket.getInputStream();   //�õ���������������
				}catch(IOException e){
					Toast.makeText(this, "��������ʧ�ܣ�", Toast.LENGTH_SHORT).show();
					return;
				}
				if(bThread==false){
					ReadThread.start();
					bThread=true;
				}else{
					bRun = true;
				}
			}
			break;
		default:break;
		}
	}

	//���������߳�
	Thread ReadThread=new Thread(){

		public void run(){
			int num = 0;
			byte[] buffer = new byte[1024];
			byte[] buffer_new = new byte[1024];

			double	BMI=0;
			float BMI_save=0;
			//	byte[] buffer_save = new byte[1024];//qzxin:���

			int i = 0;
			int n = 0;
			bRun = true;
			//�����߳�
			while(true){
				try{
					while(is.available()==0){
						while(bRun == false){}
					}
					Thread.sleep(200);
					while(true){
						num = is.read(buffer);         //��������
						n=0;

						//		String s0 = new String(buffer,0,num);
						//		fmsg+=s0;    //�����յ�����

						for(i=0;i<num;i++){
							Log.i("BUF", String.valueOf(buffer[i]));
							if(buffer[i] == (byte)0xff){
								buffer_new[n] = buffer[++i];
							}
							if(buffer[i] == (byte)0xfe){
								buffer_new[n] = buffer[++i];
							}

							n++;

						}

						//qzxin
						w_save=String.valueOf(buffer_new[0]);
						h_save="1"+String.valueOf(buffer_new[1]);
						//	buffer_new[2]=10000*buffer_new[0]/(buffer_new[1]);
						int tmp_w=Integer.parseInt(String.valueOf(buffer_new[0]));
						int tmp_h=Integer.parseInt("1"+String.valueOf(buffer_new[1]));
						BMI=10000*tmp_w/Math.pow(tmp_h, 2);
						BMI_save=(float)BMI;
						if(BMI_save<18.5)
							BMI_value="BMI:"+String.valueOf(BMI_save)+"\n"+"ƫ��";
						else if(BMI_save>24)
							BMI_value="BMI:"+String.valueOf(BMI_save)+"\n"+"���Ŭ����Ӵ";
						else
							BMI_value="BMI:"+String.valueOf(BMI_save)+"\n"+"�������뱣��";


						h_value="1"+String.valueOf(buffer_new[1])+"cm"+"\n"+BMI_value;
						w_value=String.valueOf(buffer_new[0])+"Kg"+"  "+h_value;   //д����ջ���
						if(is.available()==0)break;  //��ʱ��û�����ݲ�����������ʾ
					}
					//������ʾ��Ϣ��������ʾˢ��
					handler.sendMessage(handler.obtainMessage());       	    		
				} catch(IOException e){
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	//��Ϣ�������
	Handler handler= new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			dis.setText(w_value);   //��ʾ��������
			//	dis2.setText(h_value);
			//	dis3.setText(BMI_value);

			//sv.scrollTo(0,dis.getMeasuredHeight()); //�����������һҳ
		}
	};





	//�رճ�����ô�����

	public void onDestroy(){
		super.onDestroy();
		if(_socket!=null)  //�ر�����socket
			try{
				_socket.close();
			}catch(IOException e){}
		//	_bluetooth.disable();  //�ر���������
	}



	//���Ӱ�����Ӧ����
	public void onConnectButtonClicked(View v){ 
		if(_bluetooth.isEnabled()==false){  //����������񲻿�������ʾ
			Toast.makeText(this, " ��������...", Toast.LENGTH_LONG).show();
			return;
		}


		//��δ�����豸���DeviceListActivity�����豸����
		Button btn = (Button) findViewById(R.id.Button03);
		if(_socket==null){
			Intent serverIntent = new Intent(this, DeviceListActivity.class); //��ת��������
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //���÷��غ궨��
		}
		else{
			//�ر�����socket
			try{

				is.close();
				_socket.close();
				_socket = null;
				bRun = false;
				btn.setText("����");
			}catch(IOException e){}   
		}
		return;
	}

	//���水����Ӧ����
	public void onSaveButtonClicked(View v){
		Save();
	}

	//��ʷ������Ӧ����


	//�˳�������Ӧ����
	public void onQuitButtonClicked(View v){
		finish();
	}

	//���湦��ʵ��
	private void Save() {
		//��ʾ�Ի��������ļ���
		//		LayoutInflater factory = LayoutInflater.from(BTClient.this);  //ͼ��ģ�����������
		//	final View DialogView =  factory.inflate(R.layout.sname, null);  //��sname.xmlģ��������ͼģ��
		new AlertDialog.Builder(BTClient.this)
		.setTitle("")
		//							.setView(DialogView)   //������ͼģ��
		.setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener() //ȷ��������Ӧ����
		{
			public void onClick(DialogInterface dialog, int whichButton){
				//EditText text1 = (EditText)DialogView.findViewById(R.id.sname);  //�õ��ļ����������
				filename = "w.txt";  //�õ��ļ���

				try{
					if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //���SD����׼����

						//filename =filename+".txt";   //���ļ���ĩβ����.txt										
						File sdCardDir = Environment.getExternalStorageDirectory();  //�õ�SD����Ŀ¼
						//File BuildDir = new File(sdCardDir, "/data");   //��dataĿ¼���粻����������
						//if(BuildDir.exists()==false)BuildDir.mkdirs();
						File saveFile =new File(sdCardDir.getAbsolutePath()+File.separator+filename);  //�½��ļ���������Ѵ������½��ĵ�
						FileOutputStream stream = new FileOutputStream(saveFile,true);  //���ļ�������
						//stream.write(fmsg.getBytes());
						stream.write(w_save.getBytes());
						stream.write("a".getBytes());
						/*
						stream.write(h_save.getBytes());
						stream.write("a".getBytes());*/
						stream.close();
						Toast.makeText(BTClient.this, "�洢�ɹ���", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(BTClient.this, "û�д洢����", Toast.LENGTH_LONG).show();
					}

				}catch(IOException e){
					return;
				}



			}
		})
		.setNegativeButton("ȡ��",   //ȡ��������Ӧ����,ֱ���˳��Ի������κδ��� 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
			}
		}).show();  //��ʾ�Ի���
	} 







	private IDemoChart[] mCharts = new IDemoChart[] { new WeightLineChart()};
	//��ʷ������Ӧ����
	public void _ChartDemo(View view)
	{
		/*
	Intent intent=new Intent();
	intent.setClass(this, ChartDemo.class);
	startActivity(intent);*/


		Intent intent= mCharts[0].execute(this);
		startActivity(intent);
	}

}
