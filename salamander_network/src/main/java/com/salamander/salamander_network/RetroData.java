package com.salamander.salamander_network;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class RetroData implements Parcelable {

    private String ActivityName, ClassName, MethodName, Parameter;
    private String Result;
    private RetroStatus retroStatus = new RetroStatus();

    public RetroData() {}

    protected RetroData(Parcel in) {
        ActivityName = in.readString();
        ClassName = in.readString();
        MethodName = in.readString();
        Parameter = in.readString();
        Result = in.readString();
        retroStatus = in.readParcelable(RetroStatus.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ActivityName);
        dest.writeString(ClassName);
        dest.writeString(MethodName);
        dest.writeString(Parameter);
        dest.writeString(Result);
        dest.writeParcelable(retroStatus, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RetroData> CREATOR = new Creator<RetroData>() {
        @Override
        public RetroData createFromParcel(Parcel in) {
            return new RetroData(in);
        }

        @Override
        public RetroData[] newArray(int size) {
            return new RetroData[size];
        }
    };

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getMethodName() {
        return MethodName;
    }

    public void setMethodName(String methodName) {
        MethodName = methodName;
    }

    public String getParameter() {
        return Parameter;
    }

    public void setParameter(String parameter) {
        Parameter = parameter;
    }

    public RetroStatus getRetroStatus() {
        return retroStatus;
    }

    public void setRetroStatus(RetroStatus retroStatus) {
        this.retroStatus = retroStatus;
    }

    public boolean isSuccess() {
        return retroStatus != null && retroStatus.isSuccess();
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getErrorMsg() {
        return retroStatus.getMessage();
    }

    public JSONObject getJSONData() {
        return JSON.getJSONObject(JSON.toJSONObject(getResult()), "data");
    }
    public String getData() {
        return JSON.getStringOrNull(JSON.toJSONObject(getResult()), "data");
    }

    public String getActivityName() {
        return ActivityName;
    }

    public void setActivityName(String activityName) {
        ActivityName = activityName;
    }
}
