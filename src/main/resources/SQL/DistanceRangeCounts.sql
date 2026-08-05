SELECT distance_range,
       COUNT(*) AS ride_count
FROM (SELECT CASE
                 WHEN distance > 0 AND distance < 50 THEN '0–49 km'
                 WHEN distance >= 50 AND distance < 100 THEN '50–99 km'
                 WHEN distance >= 100 AND distance < 200 THEN '100–199 km'
                 WHEN distance >= 200 AND distance < 300 THEN '200–299 km'
                 WHEN distance >= 300 AND distance < 400 THEN '300–399 km'
                 WHEN distance >= 400 AND distance < 600 THEN '400–599 km'
                 ELSE '600+ km'
                 END AS distance_range
      FROM (SELECT round(ride_activity.distance::numeric, 1) AS distance
            FROM strava.ride_activity
                     LEFT JOIN strava.bike ON ride_activity.gear_id = bike.id
            WHERE sport_type IN ('Ride', 'MountainBikeRide', 'GravelRide')
              AND round(ride_activity.distance::numeric, 1) > 0) AS base) AS sub
GROUP BY distance_range
ORDER BY CASE distance_range
             WHEN '0–49 km' THEN 1
             WHEN '50–99 km' THEN 2
             WHEN '100–199 km' THEN 3
             WHEN '200–299 km' THEN 4
             WHEN '300–399 km' THEN 5
             WHEN '400–599 km' THEN 6
             ELSE 7
             END;
