package com.ei.zezoo.model;

import java.util.ArrayList;

public class Type {
    private int TypeN;

    public int getTypeN() {
        return TypeN;
    }

    public void setTypeN(int typeN) {
        TypeN = typeN;
    }

    public ArrayList<String> getStudiesN() {
        return StudiesN;
    }

    public void setStudiesN(ArrayList<String> studiesN) {
        StudiesN = studiesN;
    }

    public ArrayList<String> getTeachersN() {
        return TeachersN;
    }

    public void setTeachersN(ArrayList<String> teachersN) {
        TeachersN = teachersN;
    }

    public ArrayList<String> getStudentsN() {
        return StudentsN;
    }

    public void setStudentsN(ArrayList<String> studentsN) {
        StudentsN = studentsN;
    }

    public ArrayList<String> getSubjectsN() {
        return SubjectsN;
    }

    public void setSubjectsN(ArrayList<String> subjectsN) {
        SubjectsN = subjectsN;
    }

    private ArrayList<String> StudiesN,TeachersN,StudentsN,SubjectsN;

    public Type(int typeN, ArrayList<String> studiesN, ArrayList<String> teachersN, ArrayList<String> studentsN, ArrayList<String> subjectsN) {
        TypeN = typeN;
        StudiesN = studiesN;
        TeachersN = teachersN;
        StudentsN = studentsN;
        SubjectsN = subjectsN;
    }



}
