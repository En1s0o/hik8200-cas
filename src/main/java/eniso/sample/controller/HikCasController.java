package eniso.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eniso.common.hik.CasFlatRequest;
import eniso.common.hik.HikCasService;
import lombok.extern.slf4j.Slf4j;

/**
 * HikCasController
 *
 * @author Eniso
 */
@Slf4j
@RestController
@RequestMapping("/api/hik")
public class HikCasController {

    private final HikCasService hikCasService;

    @Autowired
    public HikCasController(HikCasService hikCasService) {
        this.hikCasService = hikCasService;
    }

    @PostMapping("/flatRequest")
    public ResponseEntity<Object> flatRequest(@RequestBody CasFlatRequest flatRequest) throws Exception {
        Object data = hikCasService.flatRequest(flatRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}
