package in.notyouraveragedev.tensor_image_classification;

public class Disease {
    String disease_name,produce_name,def_and_cause,solution;

    public Disease(){}

    public Disease(String disease_name, String produce_name, String def_and_cause, String solution) {
        this.disease_name = disease_name;
        this.produce_name = produce_name;
        this.def_and_cause = def_and_cause;
        this.solution = solution;
    }

    public String getDisease_name() {
        return disease_name;
    }

    public void setDisease_name(String disease_name) {
        this.disease_name = disease_name;
    }

    public String getProduce_name() {
        return produce_name;
    }

    public void setProduce_name(String produce_name) {
        this.produce_name = produce_name;
    }

    public String getDef_and_cause() {
        return def_and_cause;
    }

    public void setDef_and_cause(String def_and_cause) {
        this.def_and_cause = def_and_cause;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}
