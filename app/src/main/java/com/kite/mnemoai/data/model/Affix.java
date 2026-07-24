package com.kite.mnemoai.data.model;

import androidx.annotation.NonNull;

import java.util.Map;

public class Affix {
    private Map<String, String> prefix;
    private Map<String, String> root;
    private Map<String, String> suffix;

    public Affix(Map<String, String> prefix, Map<String, String> root, Map<String, String> suffix) {
        this.prefix = prefix;
        this.root = root;
        this.suffix = suffix;
    }

    public Map<String, String> getPrefix() {
        return prefix;
    }

    @NonNull
    @Override
    public String toString() {
        if(prefix == null) return "";
        StringBuilder builder = new StringBuilder();
        boolean shouldAddPlus = false;
        if(!root.isEmpty() || !suffix.isEmpty()) shouldAddPlus = true;
        for(Map.Entry<String, String> pre: prefix.entrySet()){
            builder.append(pre.getKey());
            builder.append(" (");
            builder.append(pre.getValue());
            builder.append(")");
            if(shouldAddPlus) builder.append(" + ");
        }
        shouldAddPlus = false;
        if(!suffix.isEmpty()) shouldAddPlus = true;
        if(root == null) return builder.toString();
        for(Map.Entry<String, String> ro: root.entrySet()){
            builder.append(ro.getKey());
            builder.append(" (");
            builder.append(ro.getValue());
            builder.append(")");
            if(shouldAddPlus) builder.append(" + ");
        }
        if(suffix == null) return builder.toString();
        for(Map.Entry<String, String> suf: suffix.entrySet()){
            builder.append(suf.getKey());
            builder.append(" (");
            builder.append(suf.getValue());
            builder.append(")");
        }

        return builder.toString().isEmpty() ? "" : builder.toString();
    }

    public void setPrefix(Map<String, String> prefix) {
        this.prefix = prefix;
    }

    public Map<String, String> getRoot() {
        return root;
    }

    public void setRoot(Map<String, String> root) {
        this.root = root;
    }

    public Map<String, String> getSuffix() {
        return suffix;
    }

    public void setSuffix(Map<String, String> suffix) {
        this.suffix = suffix;
    }
}
