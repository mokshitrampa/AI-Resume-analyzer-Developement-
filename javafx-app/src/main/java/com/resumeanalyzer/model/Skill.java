package com.resumeanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Skill {
    private Long id;
    private String name;
    private String category;
    private List<String> aliases = new ArrayList<>();

    public Skill() {}

    public Skill(Long id, String name, String category, List<String> aliases) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.aliases = aliases != null ? aliases : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases != null ? aliases : new ArrayList<>(); }
}
