package in.notyouraveragedev.tensor_image_classification;

import java.io.Serializable;

public class Data implements Serializable {
    private String name;
    private String image;
    String probability;

    public Data(String name, String image, String probability) {
        this.name = name;
        this.image = image;
        this.probability = probability;
    }

    public Data(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }
}
