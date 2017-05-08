Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/trusty64"
  config.vm.provider "virtualbox" do |v|
    v.linked_clone = true if Gem::Version.new(Vagrant::VERSION) >= Gem::Version.new('1.8.0')
    # v.memory = 512
    # v.cpus = 2
  end

  #N = 2
  #(0..N).each do |machine_id|
  machine_id=0
  N=0
    config.vm.define "akka#{machine_id}" do |v|
      v.vm.hostname = "akka#{machine_id}"
      v.vm.network "private_network", ip: "192.168.88.#{10 + machine_id}"

      # Only execute once the Ansible provisioner,
      # when all the machines are up and ready.
      if machine_id == N
        v.vm.provision "ansible" do |ansible|
          ansible.playbook = "ansible/site.yml"
        end
      end
    end
  #end

end
