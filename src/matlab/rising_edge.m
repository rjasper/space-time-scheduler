function s_ = rising_edge(s, varargin)

iscircular = ismember('circular', varargin);
islow = ismember('low', varargin);
ishigh = ismember('high', varargin);

if islow && ishigh
    error('low and high cannot be set at the same time');
end

if iscircular
    s_ = ~s(1:end) & s([2:end 1]);
else
    s_ = ~s(1:end-1) & s(2:end);
end

if ishigh
    if iscircular
        s_ = circshift(s_, [0 1]);
    else
        s_ = [false s_];
    end
elseif islow && ~iscircular
    s_ = [s_ false];
end

end