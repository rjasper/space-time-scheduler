function lines = extract_lines(Om)

N = length(Om);

lines = cell(1, N);

for i = 1:N
    XY = Om{i};
    
    P1 = XY(:, 1:end);
    P2 = XY(:, [2:end 1]);
    
    lines{i} = [P1; P2];
end

lines = [lines{:}]; % concatenate cells to single matrix
