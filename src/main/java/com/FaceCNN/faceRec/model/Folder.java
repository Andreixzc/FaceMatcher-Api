package com.FaceCNN.faceRec.model;

import java.util.ArrayList;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String folderPath;

    @Column(unique = true, nullable = true)
    private String folderPklPath;

    @Column()
    private String folderName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // @OneToOne(mappedBy = "folder", cascade = CascadeType.ALL)
    // private FolderContent folderContent;
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderContent> folderContents = new ArrayList<>();


    public UUID userId() {
        return user.getId();
    }

    public void addFolderContent(FolderContent folderContent) {
        folderContents.add(folderContent);
        folderContent.setFolder(this);
    }
}
