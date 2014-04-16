function [A, V] = vgraph(I, F, Os)

V = [I F [Os{:}]];
l = get_edges(Os);
l = [l{:}];
[pid, vid] = get_ids(Os);
pid = [0 0 pid];
vid = [1 2 vid];

vec = calc_vector(Os);
rho = calc_vector_angle(vec);
alpha = calc_vertex_angle(rho);

N = size(V, 2);

A = zeros(N, N);

% TODO: parallelize

for i = 1:N
    for j = 1:N
        if i == j
            continue
        elseif visible(V(:, i), V(:, j), l)
            % if both vertices belong to the same polygon then check visibility
            if pid(i) ~= 0 && pid(i) == pid(j) && ~visible2(Os, rho, alpha, pid(i), vid(i), vid(j))
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
