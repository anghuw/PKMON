package edu.pkmon.capture.service;

import edu.pkmon.capture.domain.CaptureRecord;
import edu.pkmon.capture.domain.Spawn;
import edu.pkmon.capture.dto.CaptureDtos.CollectionSummaryResponse;
import edu.pkmon.capture.port.SpeciesCatalog;
import edu.pkmon.capture.repository.Repositories.CaptureRecordRepository;
import edu.pkmon.capture.repository.Repositories.SpawnRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 收藏概览：只读统计，不写入、不发奖。 */
@Service
public class CollectionService {

    private final SpawnRepository spawns;
    private final CaptureRecordRepository records;
    private final SpeciesCatalog species;

    public CollectionService(SpawnRepository spawns, CaptureRecordRepository records,
                             SpeciesCatalog species) {
        this.spawns = spawns;
        this.records = records;
        this.species = species;
    }

    public CollectionSummaryResponse summary(String userId) {
        List<CaptureRecord> captured = records.findByUser(userId);
        Set<String> speciesIds = new HashSet<>();
        for (CaptureRecord record : captured) {
            spawns.findById(record.spawnId()).map(Spawn::speciesId).ifPresent(speciesIds::add);
        }
        return new CollectionSummaryResponse(speciesIds.size(), species.totalSpeciesCount(),
                captured.size());
    }
}
