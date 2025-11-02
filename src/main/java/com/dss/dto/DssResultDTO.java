package com.dss.dto;

import java.util.List;

// DTO này chứa toàn bộ kết quả trả về cho Front-end
public class DssResultDTO {
    private DiscountScenarioDTO optimalScenario; // Kịch bản tốt nhất
    private List<DiscountScenarioDTO> allScenarios; // Dữ liệu cho biểu đồ
    private String analysis; // Phân tích
    private String recommendation; // Gợi ý

    // (Thêm Constructor, Getters, Setters)

    public DssResultDTO() {
    }

    public DiscountScenarioDTO getOptimalScenario() {
        return optimalScenario;
    }

    public void setOptimalScenario(DiscountScenarioDTO optimalScenario) {
        this.optimalScenario = optimalScenario;
    }
    public List<DiscountScenarioDTO> getAllScenarios() {
        return allScenarios;
    }
    public void setAllScenarios(List<DiscountScenarioDTO> allScenarios) {
        this.allScenarios = allScenarios;
    }
    public String getAnalysis() {
        return analysis;
    }
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    public String getRecommendation() {
        return recommendation;
    }
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}