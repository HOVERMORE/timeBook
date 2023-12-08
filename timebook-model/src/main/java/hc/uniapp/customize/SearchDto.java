package hc.uniapp.customize;


import hc.uniapp.album.pojos.Album;
import hc.uniapp.image.pojos.Image;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class SearchDto implements Serializable {
    List<Album> albumList;
    List<Image> imageList;
}
