package com.tomato.thread;

import android.content.Context;
import android.util.Log;

import com.tomato.main.utils.NumberHelper;
import com.tomato.main.utils.StringUtils;
import com.tomato.tools.Parameter.SystemParameter;
import com.tomato.tools.Parameter.ThreadParameter;
import com.tomato.usbutil.SerialPortOpe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * 将原始数据读取到缓冲队列中
 */
public class ReadDataThread implements Runnable{
    ThreadParameter threadParameter;
    SerialPortOpe serialPortOpe;
    final static int CURRENT_MAX_CHANEEL=9;

    public ReadDataThread(Context parentContext){
        threadParameter= ThreadParameter.getInstance();              //获取线程参数类的对象
        serialPortOpe=SerialPortOpe.getInstance(parentContext);      //获取设备操作类的对象
    }

    @Override
    public void run(){
        int index=0;
        int nChannelNumber= SystemParameter.getInstance().nChannelNumber;
        while (threadParameter.threadFlag) {
            byte[] data=serialPortOpe.getMeasureData();         //读取长度最大为ThreadParameter.getInstance().nReadSizeWords的一个byte[]
//            Log.d("values-data.length",String.valueOf(data.length));
            if (data==null||data.length==0){
                continue;
            }
            for (int i=0;i < data.length-CURRENT_MAX_CHANEEL*2-1;i++){
                if (data[i]==0x08&&data[i+1]==0x5A&&data[i+CURRENT_MAX_CHANEEL*2]==0x08&&data[i+CURRENT_MAX_CHANEEL*2+1]==0x5A){
                    for (int j=0;j<nChannelNumber+2;j++){
                        threadParameter.ADBuffer[index]=data[i+j+1];
                        threadParameter.bNewSegmentData[index++]=true;           //对应的标志改为true
                        index %= threadParameter.MAX_SEGMENT;
                    }
                    i += (CURRENT_MAX_CHANEEL*2-1);//如果满足 i跳到下一个标记
                }
            }
        }
    }
}
