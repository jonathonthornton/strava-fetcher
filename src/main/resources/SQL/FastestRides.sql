select round(ra.average_speed::numeric, 1) as average_speed,
       round(ra.distance::numeric, 1) as distance,
       ra.name
    from strava.ride_activity ra
join strava.bike b on b.id = ra.gear_id
where b.name = 'Corretto';