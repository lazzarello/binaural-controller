class ArcEncoder:
    def __init__(self, in_min, in_max, out_min, out_max):
        self.in_min = in_min
        self.in_max = in_max
        self.out_min = out_min
        self.out_max = out_max
        self.accumulated_value = 0

    def update(self, delta):
        self.accumulated_value += delta
        return self.scale_value(self.accumulated_value)
    
    def scale_value(self, value):
        if self.in_min == self.in_max:
            raise ValueError("Input range cannot be zero")
        scaled_value = (value - self.in_min) * (self.out_max - self.out_min) / (self.in_max - self.in_min) + self.out_min
        return scaled_value
