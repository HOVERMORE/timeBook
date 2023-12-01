package hc.uniapp.image.dtos;

import hc.uniapp.image.pojos.Image;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RecentAlbumsDto  implements Serializable {
    private List<Image> recentImages;
    private List<Image> shortTimeImages;
    private List<Image> longTimeImages;
}
