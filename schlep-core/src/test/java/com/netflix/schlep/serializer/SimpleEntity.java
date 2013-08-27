package com.netflix.schlep.serializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleEntity {
    public String       str;
    public int          i;
    public Integer      oI;
    public short        s;
    public Short        oS;
    public boolean      b;
    public Boolean      oB;
    public double       d;
    public Double       oD;
    public float        f;
    public Float        oF;
    public List<String> list;
    public Map<String, String> map;
    public Set<String> set;
    public ChildEntity c;
    public List<ChildEntity> cl;

    public SimpleEntity() {
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Set<String> getSet() {
        return set;
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public ChildEntity getC() {
        return c;
    }

    public void setC(ChildEntity c) {
        this.c = c;
    }

    public List<ChildEntity> getCl() {
        return cl;
    }

    public void setCl(List<ChildEntity> cl) {
        this.cl = cl;
    }

    public void setS(short s) {
        this.s = s;
    }

    public Integer getoI() {
        return oI;
    }

    public void setoI(Integer oI) {
        this.oI = oI;
    }

    public Short getoS() {
        return oS;
    }

    public void setoS(Short oS) {
        this.oS = oS;
    }

    public Boolean getoB() {
        return oB;
    }

    public void setoB(Boolean oB) {
        this.oB = oB;
    }

    public Double getoD() {
        return oD;
    }

    public void setoD(Double oD) {
        this.oD = oD;
    }

    public Float getoF() {
        return oF;
    }

    public void setoF(Float oF) {
        this.oF = oF;
    }

    public short getS() {
        return s;
    }

}