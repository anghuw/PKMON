package edu.pkmon.capture.repository;

/** 唯一约束冲突。内存实现用它模拟数据库的唯一约束，便于业务层写出同一条冲突处理路径。 */
public class DuplicateRecordException extends RuntimeException {

    public DuplicateRecordException(String message) {
        super(message);
    }
}
