SELECT bike.name,
       TO_CHAR(MIN(ride_activity.start_date_local), 'YYYY-MM-DD') AS earliest_ride_date,
       EXTRACT(YEAR FROM AGE(MIN(ride_activity.start_date_local))) || ' years ' ||
       EXTRACT(MONTH FROM AGE(MIN(ride_activity.start_date_local))) || ' months' AS years_months_ago
FROM strava.ride_activity
JOIN strava.bike ON strava.ride_activity.gear_id = strava.bike.id
WHERE bike.name not in ('Paconi', 'Gellie')
GROUP BY bike.name
ORDER BY earliest_ride_date;
