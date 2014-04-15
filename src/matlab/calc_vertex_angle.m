function alpha = calc_vertex_angle(rho)

if iscell(rho)
    alpha = cellfun(@helper, rho, 'UniformOutput', false);
else
    alpha = helper(rho);
end

function alpha = helper(rho)

rho1 = circshift(rho, [0 1]);
rho2 = rho;

alpha = mod( rho1 - rho2 + pi, 2*pi );