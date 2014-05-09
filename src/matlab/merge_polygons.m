function [V, vid, R2V_map] = merge_polygons(V_L, vid_L, vid_S_L, V_R, vid_R, vid_S_R)

N_L = length(vid_L);
N_R = length(vid_R);
N_P_max = N_L + N_R;
n_P_max = sum([ cellfun(@length, vid_L) cellfun(@length, vid_R) ]);
n_L = size(V_L, 2);
n_R = size(V_R, 2);
n_S = length(vid_S_L); % == length(vid_S_R)
n_B = n_S - 1;

% calculate index mapping from V_R to V
non_vid_S_R = setdiff(1:n_R, vid_S_R);
R2V_map = zeros(1, n_R);
R2V_map(vid_S_R) = vid_S_L;
R2V_map(non_vid_S_R) = n_L + (1:n_R-n_S);

V = [V_L V_R(:, non_vid_S_R)];

vid = cell(1, N_P_max);

[mapTo_L, mapFrom_L] = determine_mapping(vid_L, vid_S_L, 'left');
[mapTo_R, mapFrom_R] = determine_mapping(vid_R, vid_S_R, 'right');

unused_L = true(1, N_L);
unused_R = true(1, N_R);
vid_i = zeros(1, n_P_max);
i = 1;
while any(unused_R)
    % get first unused polygon
    pid_R = find(unused_R, 1, 'first');
    vid_R_pid = vid_R{pid_R};
    isB_R = mapTo_R{pid_R} ~= 0;
    
    if ~any(isB_R) % no bridges
        unused_R(pid_R) = false;
        vid_i = R2V_map( vid_R_pid );
    else % some bridges
        % find first bridge end (previous is bridge and current is not)
        pvid_first = find(isB_R([end 1:end-1]) & ~isB_R, 1, 'first');
        vid_R_first = vid_R{pid_R}(pvid_first);
        
        no_loop = true;
        pvid_start_R = pvid_first;
        
    
        j = 1;
        while no_loop
            unused_R(pid_R) = false;
            
            n_R_pid = length(vid_R_pid);
            
            % go to next bridge start (might be cur)
            pvid_Bstart_R = bnext(isB_R, pvid_start_R);
            pvid_end_R = idxmod(pvid_Bstart_R-1, n_R_pid);
            
            I_R = interval(pvid_start_R, pvid_end_R, n_R_pid);
            n_I_R = length(I_R);
            vid_i(j-1 + (1:n_I_R)) = R2V_map( vid_R_pid(I_R) );
            j = j + n_I_R;
            
            % cross bridge to L
            
            bid = mapTo_R{pid_R}(pvid_Bstart_R);
            
            pid_L = mapFrom_L(1, bid);
            vid_L_pid = vid_L{pid_L};
            pvid_start_L = mapFrom_L(2, bid);
            isB_L = mapTo_L{pid_L} ~= 0;
            
            unused_L(pid_L) = false;
            
            n_L_pid = length(vid_L_pid);
            
            % go to next bridge start
            pvid_Bstart_L = bnext(isB_L, pvid_start_L);
            pvid_end_L = idxmod(pvid_Bstart_L-1, n_L_pid);
            
            I_L = interval(pvid_start_L, pvid_end_L, n_L_pid);
            n_I_L = length(I_L);
            vid_i(j-1 + (1:n_I_L)) = vid_L_pid( I_L );
            j = j + n_I_L;
            
            % cross bridge back to R
            
            bid = mapTo_L{pid_L}(pvid_Bstart_L);
            
            pid_R = mapFrom_R(1, bid);
            vid_R_pid = vid_R{pid_R};
            pvid_start_R = mapFrom_R(2, bid);
            isB_R = mapTo_R{pid_R} ~= 0;
            
            no_loop = vid_R_pid(pvid_start_R) ~= vid_R_first;
        end
    end
    
    vid{i} = vid_i(1:j-1);
    i = i + 1;
end

vid = [vid(1:i-1) vid_L(unused_L)];

    function [mapTo, mapFrom] = determine_mapping(vid, vid_S, side)
        % number of polygons
        N_P = length(vid);
        
        mapFrom = zeros(2, n_B);
        mapTo = cellfun(@helper, vid, num2cell(1:N_P), 'UniformOutput', false);
        
        function mapTo = helper(vid, i)
            % number of polygon vertices
            n_P = length(vid);
            
            % find vertices on the cut
            [isS, S_idx] = ismember(vid, vid_S);
            % find bridges
            B_filt = isS & isS([2:end 1]);
            B_filt_ = circshift(B_filt, [0 -1]);

            % determine bridge IDs
            switch side
                case 'left'
                    B = S_idx( B_filt );
                case 'right'
                    B = S_idx( B_filt_ );
            end
            
            mapTo = zeros(1, n_P);
            mapTo(B_idx) = B;
            
            mapFrom(1, B) = i; % polygon ID (pid)
            mapFrom(2, B) = find(B_filt_); % polygon vertex ID (pvid)
        end
    end
end

function next = bnext(b, idx)
    b_shift = circshift(b, [0 1-idx]);

    next_shift = find(b_shift, 1, 'first');
    next = mod( next_shift + idx-1 - 1, length(b) ) + 1;
end

function I = interval(i_start, i_end, n)
    if i_start <= i_end
        I = i_start:i_end;
    else
        I = [i_start:n 1:i_end];
    end
end