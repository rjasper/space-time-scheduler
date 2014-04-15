function b = visible2(Om, rho, alpha, pid, vid1, vid2)

P1 = Om{pid}(:, vid1);
P2 = Om{pid}(:, vid2);

% angle of the second line of vertex 1
rho2 = rho{pid}(vid1);
% angle of line between P1 and P2
rho = calc_line_angle([P1; P2]);

% check if rho lies between rho1 and rho2
% where rho1 is the first and rho2 is the second line of vertex 1

% angle between rho2 and rho
delta1 = mod(rho - rho2, 2*pi);
% angle between rho2 and rho1
delta2 = alpha{pid}(vid1);

if 0 < delta1 && delta1 < delta2
    b = false;
else
    b = true;
end