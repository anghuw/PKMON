package edu.pkmon.capture.web;

import edu.pkmon.capture.dto.CaptureDtos.ApiEnvelope;
import edu.pkmon.capture.port.CurrentUserProvider;
import edu.pkmon.capture.service.CollectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 最小收藏查询：验证真实持久化后的重新查询。 */
@RestController
@RequestMapping("/api/v1/collection")
public class CollectionController {

    private final CollectionService collectionService;
    private final CurrentUserProvider currentUser;

    public CollectionController(CollectionService collectionService,
                               CurrentUserProvider currentUser) {
        this.collectionService = collectionService;
        this.currentUser = currentUser;
    }

    @GetMapping("/summary")
    public ApiEnvelope summary() {
        String userId = currentUser.currentUserId();
        return ApiEnvelope.ok(collectionService.summary(userId));
    }
}
