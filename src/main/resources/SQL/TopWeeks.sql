select
    to_char(date_trunc('week', start_date_local), 'YYYY-MM-DD') as week_start,
    round(sum(distance)) as total_distance
from strava.ride_activity
where start_date_local is not null
group by date_trunc('week', start_date_local)
order by total_distance desc
limit 10;
