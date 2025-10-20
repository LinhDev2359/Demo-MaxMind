package org.aibles.demo_maxmind_geolite2.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.dto.response.DatabaseUpdateResponse;
import org.aibles.demo_maxmind_geolite2.service.DatabaseUpdateService;
import org.aibles.demo_maxmind_geolite2.service.GeoIpResponseService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseFacadeService {
    
    private final DatabaseUpdateService databaseUpdateService;
    private final GeoIpResponseService responseService;
    
    public DatabaseUpdateResponse performUpdate() {
        log.info("Manual database update requested");
        boolean success = databaseUpdateService.forceUpdate();
        return responseService.buildUpdateResponse(success);
    }
    
    public String getUpdateStatus() {
        return databaseUpdateService.getUpdateStatus();
    }
}