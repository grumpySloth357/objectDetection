# Shreeya Khadka
# Create mapping from Index-->String for each input in input protobuf file
# ==============================================================================

import numpy as np

'''Create label_map from input path to pbtxt'''
def load_labelmap(path):
  label_map = {}
  with open(path) as f:
    lines = f.readlines()
    for l in lines:
      words = l.split(':')
      words = [w.strip() for w in words] #Get rid of spaces/newlines
      if words[0]=='id':
        obj_id = np.int32(words[1])
      elif words[0]=='display_name':
        obj_name = words[1]
        label_map[obj_id] = {'id':obj_id, 'name':obj_name.strip('\"')}
        #label_map[obj_id] = obj_name.strip('\"')
      else:
        continue
  return label_map        

def main():
  lmap = load_labelmap('../data/mscoco_label_map.pbtxt')    
  print (lmap)  

#Testing..
if __name__ == "__main__":
    # execute only if run as a script
    main()
