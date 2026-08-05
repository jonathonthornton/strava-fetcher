-- All rides (dates, bike names and distances).
SELECT TO_CHAR(start_date_local, 'YYYY-MM-DD')   AS start_date,
       ride_activity.name,
       COALESCE(bike.name, 'Unknown')            AS bike_name,
       round(ride_activity.distance::numeric, 1) AS distance
FROM strava.ride_activity
         LEFT JOIN
     strava.bike ON strava.ride_activity.gear_id = strava.bike.id
WHERE sport_type in ('Ride', 'MountainBikeRide', 'GravelRide')
  AND round(ride_activity.distance::numeric, 1) > 0
ORDER BY start_date_local desc;