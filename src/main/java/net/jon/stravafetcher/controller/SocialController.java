package net.jon.stravafetcher.controller;

import net.jon.stravafetcher.dto.FollowerDTO;
import net.jon.stravafetcher.repository.CommentRepository;
import net.jon.stravafetcher.repository.KudosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/social")
public class SocialController {
    private static final Logger log = LoggerFactory.getLogger(SocialController.class);
    private static final int YEARS = 20;
    private final KudosRepository kudosRepository;
    private final CommentRepository commentRepository;

    public SocialController(KudosRepository kudosRepository, CommentRepository commentRepository) {
        this.kudosRepository = kudosRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/kudos/top/{limit}")
    public List<FollowerDTO> getTopKudosers(@PathVariable int limit) {
        log.debug("getTopKudosers called with limit={}", limit);
        return kudosRepository.findTopKudosers(
                LocalDateTime.now().minusYears(YEARS),
                PageRequest.of(0, limit)).toList();
    }

    @GetMapping("/kudos/bottom/{limit}")
    public List<FollowerDTO> getBottomKudosers(@PathVariable int limit) {
        log.debug("getBottomKudosers called with limit={}", limit);
        return kudosRepository.findBottomKudosers(
                LocalDateTime.now().minusYears(YEARS),
                PageRequest.of(0, limit)).toList();
    }

    @GetMapping("/commenters/top/{limit}")
    public List<FollowerDTO> getTopKCommenters(@PathVariable int limit) {
        log.debug("getTopKCommenters called with limit={}", limit);
        return commentRepository.findTopCommenters(
                LocalDateTime.now().minusYears(YEARS),
                PageRequest.of(0, limit)).toList();
    }
}
