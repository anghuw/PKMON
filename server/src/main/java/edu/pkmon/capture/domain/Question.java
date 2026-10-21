package edu.pkmon.capture.domain;

import java.util.List;

/** 题目。correctOptionId 仅服务端内部使用，对外响应不得暴露。 */
public record Question(
        String questionId,
        String speciesId,
        String questionText,
        List<OptionValue> options,
        String correctOptionId) {

    public record OptionValue(String optionId, String text) {
    }

    public boolean hasOption(String optionId) {
        return optionId != null && options.stream().anyMatch(o -> o.optionId().equals(optionId));
    }

    public boolean isCorrect(String optionId) {
        return correctOptionId.equals(optionId);
    }
}
