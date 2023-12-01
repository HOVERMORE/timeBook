package hc.uniapp.image.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImageDto  implements Serializable {
    private String imageId;
    private String orgAlbumId;
    private String updateAlbumId;
    private Integer type;
}
