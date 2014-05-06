function Omega = polygon_angles(P)

e = get_edges(P);
rho = calc_line_angle(e);
gamma = mod( diff(rho([1:end 1]), 1, 2), 2*pi);
filt = gamma > pi;
gamma(filt) = gamma(filt) - 2*pi;
Omega = sum(gamma);