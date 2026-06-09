package com.interview.entity.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SubmitMultiScoreDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long projectId;
    
    private Long candidateId;
    
    private Long examinerId;
    
    private Long roomId;
    
    private String comment;
    
    private List<ScoreItemDetail> itemScores;
    
    @Data
    public static class ScoreItemDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long scoreItemId;
        private BigDecimal score;
    }
}
