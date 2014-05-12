function [A, V] = vgraph(I, F, Os)

V = [I F [Os{:}]];
l = get_edges(Os);
l = [l{:}];
[pid, vid, n] = get_ids(Os);
pid = [0 0 pid];
vid = [1 2 vid];
n = [2 2 n];

vec = calc_vector(Os);
rho = calc_vector_angle(vec);
alpha = calc_vertex_angle(rho);

n_V = size(V, 2);

A = zeros(n_V, n_V);

% TODO: parallelize
% make this hideous method human readable :P

for i = 1:n_V
    for j = 1:n_V
        if i == j
            continue
        end
        
        if pid(i) == 0
            filt_i = true(1, n_V);
        else
            filt_i = pid ~= pid(i) | (vid ~= pred(i) & vid ~= vid(i));
        end
        
        if pid(j) == 0
            filt_j = true(1, n_V);
        else
            filt_j = pid ~= pid(j) | (vid ~= pred(j) & vid ~= vid(j));
        end
        
        l_filt = l(:, filt_i(3:end) & filt_j(3:end));
        
        % if neighbors
        if pid(i) ~= 0 && pid(i) == pid(j) && (pred(i) == vid(j) || pred(j) == vid(i))
            A(i, j) = sqrt( sum( (V(:, i) - V(:, j)).^2 ) );
        elseif visible(V(:, i), V(:, j), l_filt) % check line of sight
            % if both vertices belong to the same polygon then check visibility
            if pid(i) ~= 0 && pid(i) == pid(j) && ~visible2(Os, rho, alpha, pid(i), vid(i), vid(j))
                continue
            end
            
            % distance between the two vertices
            A(i, j) = sqrt( sum( (V(:, i) - V(:, j)).^2 ) );
        end
    end
end

A = sparse(A);

    function pred_vid = pred(i)
        vid_i = vid(i);
        n_i = n(i);
        
        if vid_i == 1
            pred_vid = n_i;
        else
            pred_vid = vid_i-1;
        end
    end

    function succ_vid = succ(i)
        vid_i = vid(i);
        n_i = n(i);
        
        if vid_i == n_i
            succ_vid = 1;
        else
            succ_vid = vid+1;
        end
    end
end