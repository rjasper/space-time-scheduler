function [A, V] = vgraph(I, F, Om)

V = [I F [Om{:}]];
lines = get_lines(Om);
lines = [lines{:}];
[pid, vid] = get_ids(Om);
pid = [0 0 pid];
vid = [1 2 vid];

vec = calc_vector(Om);
rho = calc_vector_angle(vec);
alpha = calc_vertex_angle(rho);

N = size(V, 2);

A = zeros(N, N);

for i = 1:N
    for j = 1:N
        if i == j
            continue
        elseif visible(V(:, i), V(:, j), lines)
            % if both vertices belong to the same polygon then check visibility
            if pid(i) ~= 0 && pid(i) == pid(j) && ~visible2(Om, rho, alpha, pid(i), vid(i), vid(j))
                continue
            end
            
            % distance between the two vertices
            A(i, j) = sqrt( sum( (V(:, i) - V(:, j)).^2 ) );
            
%             line(V(1, [i j])', V(2, [i j])');
%             hold on;
        end
    end
end

A = sparse(A);
