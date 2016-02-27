package org.openalpr.app;

import java.io.Serializable;

/**
 * Created by sujay on 23/09/14.
 */
public class AlprCandidate implements Serializable {
    private String plate;
    private double confidence;

    public void setPlate(String plate) {this.plate = plate;}

    public void setConfidence(double confidence) {this.confidence = confidence;}

    public String getPlate() {return plate;}

    public double getConfidence() {return confidence;}


}
