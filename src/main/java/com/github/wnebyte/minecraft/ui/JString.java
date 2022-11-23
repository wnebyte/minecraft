package com.github.wnebyte.minecraft.ui;

public class JString implements Comparable<JString>, CharSequence {

    private StringBuilder data;

    public JString() {
        this("");
    }

    public JString(String value) {
        this.data = new StringBuilder(value);
    }

    public void append(char c) {
        data.append(c);
    }

    public void deleteLast() {
        int length = data.length();
        if (length > 0) {
            data.delete(length - 1, length);
        }
    }

    public String get() {
        return data.toString();
    }

    @Override
    public int length() {
        return data.length();
    }

    @Override
    public char charAt(int index) {
        return data.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return data.subSequence(start, end);
    }

    @Override
    public int compareTo(JString o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.get(), o.get());
    }
}
