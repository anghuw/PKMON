package edu.pkmon.capture.port;

import java.util.Optional;

/** 物种目录端口。图鉴与物种归属他人主责，本模块只读。 */
public interface SpeciesCatalog {

    Optional<String> nameOf(String speciesId);

    int totalSpeciesCount();
}
