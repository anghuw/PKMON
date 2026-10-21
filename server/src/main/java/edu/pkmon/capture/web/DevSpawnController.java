package edu.pkmon.capture.web;

import edu.pkmon.capture.dto.CaptureDtos.ApiEnvelope;
import edu.pkmon.capture.dto.CaptureDtos.CreateSpawnRequest;
import edu.pkmon.capture.dto.CaptureDtos.SpawnResponse;
import edu.pkmon.capture.port.CurrentUserProvider;
import edu.pkmon.capture.service.SpawnService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 开发遭遇入口，仅 dev/test 开放；是否开放由 pkmon.capture.dev-spawn-enabled 控制。 */
@RestController
@RequestMapping("/api/v1/dev")
public class DevSpawnController {

    private final SpawnService spawnService;
    private final CurrentUserProvider currentUser;

    public DevSpawnController(SpawnService spawnService, CurrentUserProvider currentUser) {
        this.spawnService = spawnService;
        this.currentUser = currentUser;
    }

    @PostMapping("/spawns")
    public ApiEnvelope create(@RequestBody CreateSpawnRequest request) {
        String userId = currentUser.currentUserId();
        return ApiEnvelope.ok(SpawnResponse.from(
                spawnService.createDevSpawn(userId, request.regionId(), request.speciesId())));
    }
}
