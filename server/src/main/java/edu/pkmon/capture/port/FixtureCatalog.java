package edu.pkmon.capture.port;

import edu.pkmon.capture.domain.Question;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 题库与物种目录的固定数据替身，仅用于开发与测试。
 * 真实题库与图鉴由闫昕主责；本类不代表它们已实现，也不能用于冒充真实判题数据来源。
 */
public final class FixtureCatalog implements QuestionSource, SpeciesCatalog {

    private static final Question HELLO_WORLD = new Question(
            "hello_world_001",
            "siege_lion",
            "执行 print(\"hello world\") 之后，控制台会输出什么？",
            List.of(
                    new Question.OptionValue("A", "hello world"),
                    new Question.OptionValue("B", "\"hello world\"（连引号一起输出）"),
                    new Question.OptionValue("C", "什么都不输出，只是定义了一个字符串")),
            "A");

    private static final Map<String, String> SPECIES_NAMES = Map.of("siege_lion", "攻城狮");

    private static final Map<String, Question> QUESTIONS = Map.of("siege_lion", HELLO_WORLD);

    @Override
    public Optional<Question> findBySpeciesId(String speciesId) {
        return Optional.ofNullable(QUESTIONS.get(speciesId));
    }

    @Override
    public Optional<String> nameOf(String speciesId) {
        return Optional.ofNullable(SPECIES_NAMES.get(speciesId));
    }

    @Override
    public int totalSpeciesCount() {
        return SPECIES_NAMES.size();
    }
}
