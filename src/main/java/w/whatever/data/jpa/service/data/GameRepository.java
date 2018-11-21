/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package w.whatever.data.jpa.service.data;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import w.whatever.data.jpa.domain.Game;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "games", path = "games")
public interface GameRepository extends PagingAndSortingRepository<Game, Long> {

	Game findBySeasonAndScoringPeriodAndTeamNumber(Integer season, Integer scoringPeriod, Integer teamNumber);

	Iterable<Game> findByTeamNumber(Integer teamNumber);

	@Query("SELECT g FROM Game g WHERE NOT g.season = '2005'")
	Page<Game> findHighestScoringGames(org.springframework.data.domain.Pageable pageable);

	@Query("SELECT g FROM Game g WHERE NOT g.season = '2005' AND g.season >= :startSeason AND g.scoringPeriod >= :startScoringPeriod")
	Page<Game> findHighestScoringGames(@Param("startSeason") int startSeason, @Param("startScoringPeriod") int startScoringPeriod, org.springframework.data.domain.Pageable pageable);

	@Query(
			"SELECT g FROM Game g " +
					"WHERE NOT g.season = '2005' " +
					"AND g.season >= :startSeason " +
					"AND g.scoringPeriod >= :startWeek " +
					"AND g.season <= :endSeason " +
					"AND g.scoringPeriod <= :endWeek " +
					"AND g.teamNumber IN :teamNumbers " +
					"AND ((g.win = true AND :wins = true) OR " +
					"(g.loss = true AND :losses = true) OR " +
					"(g.tie = true AND :ties = true)) " +
					"AND ((:ruxbees = false) OR (:ruxbees = true AND g.ruxbee = true)) " +
					"AND ((:bugtons = false) OR (:bugtons = true AND g.bugton = true))"
	)
	Page<Game> findHighestScoringGames(
			@Param("startSeason") int startSeason,
			@Param("startWeek") int startWeek,
			@Param("endSeason") int endSeason,
			@Param("endWeek") int endWeek,
			@Param("teamNumbers") List<Integer> teamNumbers,
			@Param("wins") boolean wins,
			@Param("losses") boolean losses,
			@Param("ties") boolean ties,
			@Param("ruxbees") boolean ruxbees,
			@Param("bugtons") boolean bugtons,
			org.springframework.data.domain.Pageable pageable);

	@Query("SELECT g FROM Game g WHERE NOT g.season = '2005' and g.teamNumber = :teamNumber and g.points >= :points ORDER BY g.points DESC")
	Iterable<Game> findGamesAbovePoints(@Param("teamNumber") int teamNumber, @Param("points") int points);
}
