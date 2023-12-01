package hc.uniapp.album.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaveAlbumDto implements Serializable {
    private String userId;
    private String albumName;
}
