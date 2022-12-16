# Created by following the tutorial by Joe James
# https://youtu.be/9HFbhPscPU0
class Hashmap:
    def __init__(self):
        self.size = 6
        self.map = [None] * self.size #everycell with fixed length but nothing in it [None]
    
    def _get_hash(self, key):
        hash = 0
        for char in str(key):
            hash += ord(char)

        return hash % self.size

    def add(self, key, value):
        key_hash = self._get_hash(key)
        key_value = [key, value] #constructs a list

        if self.map[key_hash] is None: #is empty
            self.map[key_hash] = list([key_value]) #putting a list inside a list of the bigger hashmap list
            return True
        else: #already existing, update value
            for pair in self.map[key_hash]: #iterate through pairs
                if pair[0] == key:
                    pair[1] = value
                    return True
            self.map[key_hash].append(key_value)# if didnt find match, append to list
            return True

    def get(self, key):
        key_hash = self._get_hash(key)
        if self.map[key_hash] is not None: #is not empty
            for pair in self.map[key_hash]:
                if pair[0] == key:
                    return pair[1]
        return None

    def delete(self, key):
        key_hash = self._get_hash(key)

        if self.map[key_hash] is None:
            return False
        for i in range(0, len(self.map[key_hash])): #iterate through list in cell. Using range becasue index needed for deletion
            if self.map[key_hash][i][0] == key:
                self.map[key_hash].pop(i)
                return True
    
    def print(self):
        for item in self.map:
            if item is not None:
                print(str(item))