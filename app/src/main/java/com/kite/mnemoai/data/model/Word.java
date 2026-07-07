package com.kite.mnemoai.data.model;

public class Word {
    private long id;
    private String word;

    private String phonetic;
    
    private String definition;
    
    private String translation;

    private String pos;

    private Integer collins;

    private Integer oxford;

    private String tag;

    private Integer bnc;

    private Integer frq;

    private String exchange;

    private String detail;

    private String audio;

    public Word(long id, String word, String phonetic, String definition,
                      String translation, String pos, Integer collins, Integer oxford,
                      String tag, Integer bnc, Integer frq, String exchange,
                      String detail, String audio) {
        this.id = id;
        this.word = word;
        this.phonetic = phonetic;
        this.definition = definition;
        this.translation = translation;
        this.pos = pos;
        this.collins = collins;
        this.oxford = oxford;
        this.tag = tag;
        this.bnc = bnc;
        this.frq = frq;
        this.exchange = exchange;
        this.detail = detail;
        this.audio = audio;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getPos() { return pos; }
    public void setPos(String pos) { this.pos = pos; }

    public Integer getCollins() { return collins; }
    public void setCollins(Integer collins) { this.collins = collins; }

    public Integer getOxford() { return oxford; }
    public void setOxford(Integer oxford) { this.oxford = oxford; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public Integer getBnc() { return bnc; }
    public void setBnc(Integer bnc) { this.bnc = bnc; }

    public Integer getFrq() { return frq; }
    public void setFrq(Integer frq) { this.frq = frq; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }
}
