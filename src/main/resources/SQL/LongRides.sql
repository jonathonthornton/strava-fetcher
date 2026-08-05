SELECT TO_CHAR(start_date_local, 'YYYY-MM-DD')   AS start_date,
       ride_activity.name,
       bike.name,
       ROUND(ride_activity.distance::numeric, 1) AS ride_distance,
       ROUND(total_elevation_gain)               AS total_elevation_gain
FROM strava.ride_activity
         JOIN strava.bike ON strava.ride_activity.gear_id = strava.bike.id
WHERE ride_activity.distance >= 200
ORDER BY ride_distance DESC;
