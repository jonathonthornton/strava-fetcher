WITH filtered_rides AS (SELECT TO_CHAR(start_date_local, 'YYYY-MM-DD') AS ride_date,
                               round(MAX(distance)::numeric, 1)        AS km
                        FROM strava.ride_activity
                        WHERE sport_type IN ('Ride', 'MountainBikeRide', 'GravelRide')
                          AND distance > 0
                        GROUP BY TO_CHAR(start_date_local, 'YYYY-MM-DD')),
     all_possible_e AS (SELECT generate_series(1, 1000) AS e),
     candidates AS (SELECT e.e
                    FROM all_possible_e e
                             JOIN (SELECT km
                                   FROM filtered_rides) f ON f.km >= e.e
                    GROUP BY e.e
                    HAVING COUNT(*) >= e.e)
SELECT MAX(e) AS eddington_number
FROM candidates;
